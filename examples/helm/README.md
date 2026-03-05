# Helm Example - App of Apps

Deploy workloads using an ArgoCD App of Apps pattern. The master chart generates ArgoCD Applications for each enabled component.

## Quick Start

1. Copy this folder to your own repository
2. Edit `values.yaml` - enable/disable components under `components:`
3. Push to your Git repository
4. Order the **Field Content CI** from RHDP with your repository URL

## Architecture

```
helm/
├── Chart.yaml
├── values.yaml              # Central configuration for all components
├── templates/
│   └── applications.yaml    # Generates ArgoCD Applications per component
└── components/
    ├── operator/            # OLM operator subscription
    ├── hello-world/         # Sample demo application
    └── showroom/            # Lab guide deployment
```

Each component under `components/` is a standalone Helm chart. The master chart creates ArgoCD Applications that deploy these child charts.

## Components

| Component | Description | Default |
|-----------|-------------|---------|
| `connectivityLinkOperators` | Import operators from [connectivity-link](https://gitlab.com/maximilianoPizarro/connectivity-link) (RHCL, Service Mesh, Dev Spaces, RHBK, RHDH, OpenTelemetry, Pipelines, Grafana, Kiali) | disabled |
| `operator` | Install operators via OLM | disabled |
| `helloWorld` | Sample httpd application | enabled |
| `showroom` | Lab guide with terminal | enabled |

### Connectivity Link Operators

Import the operators Helm chart from [connectivity-link](https://gitlab.com/maximilianoPizarro/connectivity-link/-/blob/main/operators/Chart.yaml):

```yaml
# In values.yaml
components:
  connectivityLinkOperators:
    enabled: true
    repoUrl: "https://gitlab.com/maximilianoPizarro/connectivity-link.git"
    revision: "main"
    path: "operators"
```

The chart deploys OLM Subscriptions for Service Mesh, Grafana, Kiali, Cluster Observability, plus standalone templates for RHCL, OpenTelemetry, DevSpaces, RHBK, RHDH, and Pipelines operators. Customize `subscriptions` in values to add/remove operators.

## Configuration

All settings are in `values.yaml`. Override via Helm values:

```bash
# Enable operator installation
helm template . --set components.operator.enabled=true

# Use a different lab guide repository
helm template . --set components.showroom.content.repoUrl=https://github.com/your-org/your-lab.git
```

See comments in [values.yaml](values.yaml) and [templates/applications.yaml](templates/applications.yaml) for detailed documentation.

## Testing Locally

```bash
helm lint .
helm template my-release . --set deployer.domain=apps.cluster.example.com
```

## Adding Components

1. Create a new chart in `components/your-component/`
2. Add configuration under `components.yourComponent` in `values.yaml`
3. Add an Application block in `templates/applications.yaml`
