# LLM Serving Backup (GPU cluster)

Backup of manifests from the OpenShift AI cluster that serves the vLLM model
used by OpenShift Lightspeed in the workshop clusters.

**Cluster**: `api.ocp.xbgvw.sandbox67.opentlc.com`
**Namespace**: `my-first-model`
**Model**: Qwen 2.5 7B Instruct (vLLM on NVIDIA GPU)

## Files

| File | Description |
|------|-------------|
| `serving-runtime.yaml` | vLLM CUDA ServingRuntime for KServe |
| `inference-service.yaml` | InferenceService (RawDeployment mode, 1x GPU) |
| `route.yaml` | Edge Route to expose the `/v1` OpenAI-compatible API |

## Usage

These manifests are **NOT** managed by ArgoCD. They are a reference backup
for manual deployment when a GPU-enabled sandbox cluster is available.

```bash
oc new-project my-first-model
oc apply -f serving-runtime.yaml
oc apply -f inference-service.yaml
# Wait for the model to load (~2-3 min)
oc apply -f route.yaml
```

Then update `lightspeed.llmEndpoint` in the workshop `values.yaml` to point
to the new route hostname.

## Requirements

- OpenShift AI (RHOAI) with KServe
- NVIDIA GPU Operator
- At least 1x NVIDIA GPU (A10G or better) with 16 GiB VRAM
