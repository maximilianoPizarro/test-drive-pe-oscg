#!/bin/bash
# =============================================================================
# update-cluster-domain.sh
#
# Updates the OpenShift cluster domain in values.yaml (the single source of
# truth) and in static files that cannot use Helm templating: documentation,
# catalog-info.yaml, and Backstage software-template defaults.
#
# Helm templates are NOT touched — they derive the domain at render time from
# deployer.domain via {{ .Values.clusterDomain }}.
#
# Usage:
#   ./update-cluster-domain.sh <NEW_CLUSTER_DOMAIN>
#
# Example:
#   ./update-cluster-domain.sh apps.cluster-abc12.dynamic.redhatworkshops.io
#
# What it does:
#   1. Updates deployer.domain in the parent values.yaml
#   2. Replaces the old domain in static files (docs, catalog-info, software-
#      template defaults) that cannot be Helm-templated
#   3. Prints a summary of files changed
#
# After running this script:
#   - Push changes to Git so ArgoCD syncs the new domain
#   - ArgoCD propagates deployer.domain → clusterDomain to all components
#   - The showroom uses __CLUSTER_DOMAIN__ placeholder (no changes needed)
# =============================================================================

set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HELM_DIR="${SCRIPT_DIR}/examples/helm"
DOCS_DIR="${SCRIPT_DIR}/docs"

# --- Validate input ---
if [ $# -lt 1 ]; then
    echo -e "${RED}Error: Missing required argument.${NC}"
    echo ""
    echo "Usage: $0 <NEW_CLUSTER_DOMAIN>"
    echo ""
    echo "Example:"
    echo "  $0 apps.cluster-abc12.dynamic.redhatworkshops.io"
    exit 1
fi

NEW_DOMAIN="$1"

# Validate domain format
if [[ ! "$NEW_DOMAIN" =~ ^apps\. ]]; then
    echo -e "${YELLOW}Warning: Domain doesn't start with 'apps.' — are you sure?${NC}"
    echo "  Expected format: apps.cluster-XXXXX.dynamic.redhatworkshops.io"
    read -p "  Continue anyway? [y/N] " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# --- Detect current domain from static files ---
echo -e "${CYAN}Detecting current cluster domain...${NC}"

CURRENT_DOMAIN=$(grep -r -oh 'apps\.cluster-[a-z0-9]*\.dynamic\.redhatworkshops\.io' \
    "${DOCS_DIR}" "${HELM_DIR}" \
    --include="*.md" --include="*.yaml" --include="*.yml" 2>/dev/null \
    | sort | uniq -c | sort -rn | head -1 | awk '{print $2}')

if [ -z "$CURRENT_DOMAIN" ]; then
    echo -e "${YELLOW}No existing cluster domain found in static files.${NC}"
    echo "  This may be a fresh clone. Setting domain for the first time."
    CURRENT_DOMAIN="apps.cluster.example.com"
fi

if [ "$CURRENT_DOMAIN" = "$NEW_DOMAIN" ]; then
    echo -e "${GREEN}Domain is already set to ${NEW_DOMAIN}. Nothing to do.${NC}"
    exit 0
fi

echo -e "  Current: ${RED}${CURRENT_DOMAIN}${NC}"
echo -e "  New:     ${GREEN}${NEW_DOMAIN}${NC}"
echo ""

# --- Step 1: Update deployer.domain in parent values.yaml ---
echo -e "${CYAN}Step 1: Updating deployer.domain in values.yaml...${NC}"

CHANGED=0
PARENT_VALUES="${HELM_DIR}/values.yaml"
if [ -f "$PARENT_VALUES" ]; then
    if grep -q 'domain: ""' "$PARENT_VALUES" 2>/dev/null; then
        sed -i "s|domain: \"\"|domain: \"${NEW_DOMAIN}\"|" "$PARENT_VALUES"
        echo -e "  ${GREEN}✓${NC} examples/helm/values.yaml (deployer.domain)"
        CHANGED=$((CHANGED + 1))
    elif grep -q "domain:" "$PARENT_VALUES" 2>/dev/null; then
        sed -i "s|domain:.*|domain: \"${NEW_DOMAIN}\"|" "$PARENT_VALUES"
        echo -e "  ${GREEN}✓${NC} examples/helm/values.yaml (deployer.domain updated)"
        CHANGED=$((CHANGED + 1))
    fi
fi
echo ""

# --- Step 2: Update static files (docs, catalog-info, software-template defaults) ---
echo -e "${CYAN}Step 2: Updating static files (docs, catalog-info, software-templates)...${NC}"

STATIC_FILES=""

# Documentation (Markdown)
if [ -d "$DOCS_DIR" ]; then
    DOCS_MATCHES=$(grep -r -l "$CURRENT_DOMAIN" "${DOCS_DIR}" \
        --include="*.md" 2>/dev/null || true)
    if [ -n "$DOCS_MATCHES" ]; then
        STATIC_FILES="${STATIC_FILES}${DOCS_MATCHES}"$'\n'
    fi
fi

# Catalog-info.yaml and docs inside components
CATALOG_MATCHES=$(grep -r -l "$CURRENT_DOMAIN" "${HELM_DIR}" \
    --include="catalog-info.yaml" 2>/dev/null || true)
if [ -n "$CATALOG_MATCHES" ]; then
    STATIC_FILES="${STATIC_FILES}${CATALOG_MATCHES}"$'\n'
fi

# Component docs (Markdown under components/)
COMPONENT_DOCS=$(grep -r -l "$CURRENT_DOMAIN" "${HELM_DIR}/components" \
    --include="*.md" 2>/dev/null || true)
if [ -n "$COMPONENT_DOCS" ]; then
    STATIC_FILES="${STATIC_FILES}${COMPONENT_DOCS}"$'\n'
fi

# Software-template defaults
TEMPLATE_MATCHES=$(grep -r -l "$CURRENT_DOMAIN" "${HELM_DIR}/software-templates" \
    --include="*.yaml" --include="*.yml" 2>/dev/null || true)
if [ -n "$TEMPLATE_MATCHES" ]; then
    STATIC_FILES="${STATIC_FILES}${TEMPLATE_MATCHES}"$'\n'
fi

# README at repo root
if [ -f "${SCRIPT_DIR}/README.md" ] && grep -q "$CURRENT_DOMAIN" "${SCRIPT_DIR}/README.md" 2>/dev/null; then
    STATIC_FILES="${STATIC_FILES}${SCRIPT_DIR}/README.md"$'\n'
fi

# Deduplicate and process
STATIC_FILES=$(echo "$STATIC_FILES" | sort -u | grep -v '^$' || true)

if [ -n "$STATIC_FILES" ]; then
    while IFS= read -r file; do
        if [ -f "$file" ]; then
            sed -i "s|${CURRENT_DOMAIN}|${NEW_DOMAIN}|g" "$file"
            REL_PATH="${file#${SCRIPT_DIR}/}"
            echo -e "  ${GREEN}✓${NC} ${REL_PATH}"
            CHANGED=$((CHANGED + 1))
        fi
    done <<< "$STATIC_FILES"
else
    echo -e "  ${YELLOW}No static files contain the current domain.${NC}"
fi

echo ""
echo -e "${GREEN}Done!${NC} Updated ${CHANGED} files."
echo ""

# --- Summary ---
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  Cluster Domain Migration Summary${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  Old domain: ${RED}${CURRENT_DOMAIN}${NC}"
echo -e "  New domain: ${GREEN}${NEW_DOMAIN}${NC}"
echo -e "  Files updated: ${CYAN}${CHANGED}${NC}"
echo ""
echo -e "${CYAN}  How it works now:${NC}"
echo -e "  - deployer.domain in values.yaml is the single source of truth"
echo -e "  - ArgoCD passes it as clusterDomain to all Helm components"
echo -e "  - This script only updates docs, catalog-info & software-template defaults"
echo ""
echo -e "${CYAN}  Key URLs for the new cluster:${NC}"
echo -e "  Developer Hub:  https://backstage-developer-hub-developer-hub.${NEW_DOMAIN}"
echo -e "  Gitea:          https://gitea.${NEW_DOMAIN}"
echo -e "  ArgoCD:         https://openshift-gitops-server-openshift-gitops.${NEW_DOMAIN}"
echo -e "  DevSpaces:      https://devspaces.${NEW_DOMAIN}"
echo -e "  Keycloak:       https://rhbk.${NEW_DOMAIN}"
echo -e "  Showroom:       https://showroom-showroom.${NEW_DOMAIN}"
echo ""
echo -e "${CYAN}  Next steps:${NC}"
echo -e "  1. Review changes:  ${YELLOW}git diff${NC}"
echo -e "  2. Commit:          ${YELLOW}git add -A && git commit -m 'chore: migrate to ${NEW_DOMAIN}'${NC}"
echo -e "  3. Push:            ${YELLOW}git push${NC}"
echo -e "  4. ArgoCD will auto-sync the new domain to the cluster"
echo -e "  5. Restart showroom: ${YELLOW}oc rollout restart deployment/showroom -n showroom${NC}"
echo ""
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
