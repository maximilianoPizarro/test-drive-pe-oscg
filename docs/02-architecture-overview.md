---
layout: default
title: "Arquitectura general del workshop"
nav_order: 3
---

Esta página describe la arquitectura lógica del escenario Neuralbank: cómo encajan Red Hat Developer Hub, GitOps, CI/CD, identidad, exposición segura de APIs, MCP Gateway y asistencia IA frente a un clúster OpenShift.

## Flujo interactivo 3D

<div id="mcp-flow-3d" style="width:100%;border-radius:8px;overflow:hidden;margin:16px 0;border:1px solid #333;"></div>
<script>document.addEventListener('DOMContentLoaded', function(){ initMCPFlow3D('mcp-flow-3d'); });</script>

> Arrastrá con el mouse para rotar la escena. Las partículas cyan representan el flujo de datos entre componentes: desde el Developer, pasando por Developer Hub, Gitea, ArgoCD/Tekton, hasta el Gateway y los MCP servers, con el flujo paralelo de Lightspeed hacia el LLM.

## Vista de componentes

```mermaid
graph TB
    DEV["👤 Desarrollador"]

    subgraph CONSOLE ["🖥️ Consola OpenShift + Console Links"]
        CL_HUB["Developer Hub"]
        CL_ARGO["ArgoCD"]
        CL_GITEA["Gitea"]
        CL_KC["Keycloak"]
        CL_GRAFANA["Grafana"]
        CL_KIALI["Kiali"]
        CL_MAIL["Mailpit"]
    end

    subgraph PLATFORM ["🔴 Plataforma OpenShift"]
        HUB["Red Hat<br/>Developer Hub"]
        KC["Keycloak<br/>SSO / OIDC"]
        GITEA["Gitea<br/>SCM interno"]
        ARGO["Argo CD<br/>GitOps"]
        TEKTON["Tekton<br/>CI/CD Pipelines"]
        DS["Dev Spaces<br/>IDE cloud"]
    end

    subgraph AI ["🧠 IA & MCP"]
        LS["Lightspeed<br/>Plugin UI"]
        LCS["Lightspeed Core<br/>Service"]
        LLAMA["Llama Stack<br/>+ RAG"]
        LITELLM["LiteLLM<br/>Proxy"]
        MODEL["Modelo LLM<br/>(vLLM)"]
        OLS["OpenShift<br/>Lightspeed"]
        MCP_K8S["k8s-mcp-server"]
        MCP_OCP["ocp-mcp-server"]
    end

    subgraph NEURALBANK ["🏦 Neuralbank Stack  (user-neuralbank)"]
        MCP["customer-service-mcp<br/>Quarkus MCP Server"]
        BACK["neuralbank-backend<br/>REST API créditos"]
        FRONT["neuralbank-frontend<br/>SPA visualización"]
    end

    subgraph NETWORKING ["🌐 MCP Gateway · Kuadrant · Istio"]
        GW["Gateway<br/>Listener TLS"]
        HR["HTTPRoute"]
        OIDC["OIDCPolicy /<br/>AuthPolicy"]
        RL["RateLimitPolicy"]
        MCPGW["MCPGateway<br/>Extension"]
        MCPSR["MCPServer<br/>Registration"]
    end

    DEV --> CONSOLE
    CL_HUB --> HUB
    DEV --> DS
    DEV --> LS
    DS --> GITEA
    HUB --> GITEA
    HUB --> KC
    HUB --> LS
    LS --> LCS
    LCS --> LLAMA
    LLAMA --> LITELLM
    LITELLM --> MODEL
    OLS --> LITELLM
    OLS --> MCP_K8S
    OLS --> MCP_OCP
    MCPGW --> MCPSR
    MCPSR --> MCP_K8S
    MCPSR --> MCP_OCP
    GITEA --> ARGO
    GITEA --> TEKTON
    ARGO --> MCP
    ARGO --> BACK
    ARGO --> FRONT
    TEKTON --> MCP
    TEKTON --> BACK
    TEKTON --> FRONT
    MCP --> BACK
    FRONT --> BACK
    GW --> HR
    HR --> MCP
    HR --> BACK
    GW --> OIDC
    GW --> RL
    KC -.-> OIDC

    style DEV fill:#151515,color:#fff,stroke:#EE0000,stroke-width:2px
    style CL_HUB fill:#EE0000,color:#fff
    style CL_ARGO fill:#ef7b4d,color:#fff
    style CL_GITEA fill:#609926,color:#fff
    style CL_KC fill:#4078c0,color:#fff
    style CL_GRAFANA fill:#ef7b4d,color:#fff
    style CL_KIALI fill:#6a1b9a,color:#fff
    style CL_MAIL fill:#0066CC,color:#fff
    style HUB fill:#EE0000,color:#fff,stroke:#151515
    style KC fill:#4078c0,color:#fff,stroke:#151515
    style GITEA fill:#609926,color:#fff,stroke:#151515
    style ARGO fill:#ef7b4d,color:#fff,stroke:#151515
    style TEKTON fill:#fd495c,color:#fff,stroke:#151515
    style DS fill:#6a1b9a,color:#fff,stroke:#151515
    style LS fill:#0066CC,color:#fff,stroke:#151515
    style LCS fill:#0066CC,color:#fff
    style LLAMA fill:#6a1b9a,color:#fff
    style LITELLM fill:#ef7b4d,color:#fff
    style MODEL fill:#609926,color:#fff
    style OLS fill:#EE0000,color:#fff
    style MCP_K8S fill:#0066CC,color:#fff
    style MCP_OCP fill:#0066CC,color:#fff
    style MCP fill:#6a1b9a,color:#fff,stroke:#151515
    style BACK fill:#EE0000,color:#fff,stroke:#151515
    style FRONT fill:#0066CC,color:#fff,stroke:#151515
    style GW fill:#0066CC,color:#fff,stroke:#151515
    style HR fill:#0066CC,color:#fff,stroke:#151515
    style OIDC fill:#4078c0,color:#fff,stroke:#151515
    style RL fill:#ef7b4d,color:#fff,stroke:#151515
    style MCPGW fill:#609926,color:#fff
    style MCPSR fill:#609926,color:#fff
    style CONSOLE fill:#2a2a2a,color:#fff,stroke:#EE0000,stroke-width:2px
    style PLATFORM fill:#1a1a1a,color:#fff,stroke:#EE0000,stroke-width:2px
    style AI fill:#1a1a1a,color:#fff,stroke:#6a1b9a,stroke-width:2px
    style NEURALBANK fill:#1a1a1a,color:#fff,stroke:#0066CC,stroke-width:2px
    style NETWORKING fill:#1a1a1a,color:#fff,stroke:#0066CC,stroke-width:2px
```

## Console Links: acceso directo desde OpenShift

La consola de OpenShift incorpora **Console Links** como middleware de navegación, proporcionando acceso directo a todas las herramientas del workshop desde el menú de aplicaciones:

```mermaid
graph LR
    subgraph CONSOLE_MENU ["📱 Menú de Aplicaciones OpenShift"]
        direction TB
        subgraph TOOLS ["Workshop Tools"]
            T1["Developer Hub"]
            T2["ArgoCD (GitOps)"]
            T3["Gitea"]
            T4["Keycloak"]
            T5["Workshop Registration"]
            T6["Mailpit"]
        end
        subgraph OBS ["Observability"]
            O1["Grafana"]
            O2["Kiali"]
            O3["Jaeger"]
        end
    end

    T1 --> |"ConsoleLink CR"| API["OpenShift API"]
    O1 --> |"ConsoleLink CR"| API

    style CONSOLE_MENU fill:#1a1a1a,color:#fff,stroke:#EE0000,stroke-width:2px
    style TOOLS fill:#2a2a2a,color:#fff,stroke:#0066CC
    style OBS fill:#2a2a2a,color:#fff,stroke:#ef7b4d
    style API fill:#EE0000,color:#fff
    style T1 fill:#EE0000,color:#fff
    style T2 fill:#ef7b4d,color:#fff
    style T3 fill:#609926,color:#fff
    style T4 fill:#4078c0,color:#fff
    style T5 fill:#0066CC,color:#fff
    style T6 fill:#0066CC,color:#fff
    style O1 fill:#ef7b4d,color:#fff
    style O2 fill:#6a1b9a,color:#fff
    style O3 fill:#0066CC,color:#fff
```

Cada `ConsoleLink` es un Custom Resource de OpenShift que inyecta enlaces en la barra de navegación. El workshop los genera automáticamente desde Helm, asegurando que todas las herramientas estén accesibles sin necesidad de buscar URLs manualmente.

## Flujo principal: de la plantilla al despliegue

```mermaid
sequenceDiagram
    actor Dev as Desarrollador
    participant Console as OpenShift Console<br/>(Console Links)
    participant Hub as Developer Hub
    participant Git as Gitea
    participant Argo as Argo CD
    participant Tek as Tekton
    participant OCP as OpenShift
    participant Notif as Notifications<br/>+ Mailpit

    Dev->>Console: 1. Accede vía ConsoleLink
    Console->>Hub: 2. Abre Developer Hub
    Dev->>Hub: 3. Ejecuta Software Template
    Hub->>Git: 4. Crea repo con código + manifiestos
    Hub->>Hub: 5. Registra componente en catálogo
    Hub->>Notif: 6. Envía notificación (in-app + email)
    Git-->>Argo: 7. Detecta nueva Application
    Argo->>OCP: 8. Sincroniza manifiestos
    Git-->>Tek: 9. Dispara PipelineRun
    Tek->>Tek: 10. git-clone → build → deploy
    Tek->>OCP: 11. Deploy a namespace
    OCP-->>Dev: 12. Servicio accesible vía Route/Gateway
```

## Namespace por usuario y naming convention

Cada usuario recibe su propio namespace. Los componentes usan un **nombre único** con prefijo del owner para evitar colisiones:

| Recurso | Convención | Ejemplo (user1) |
| --- | --- | --- |
| Namespace | `owner-neuralbank` | `user1-neuralbank` |
| Componente catálogo | `owner-name` | `user1-neuralbank-backend` |
| Aplicación ArgoCD | `owner-name` | `user1-neuralbank-backend` |
| Kubernetes ID | `owner-name` | `user1-neuralbank-backend` |

## MCP Gateway (Extensión Comunitaria de Kuadrant)

El workshop integra **MCP Gateway**, una extensión comunitaria del operador Kuadrant que permite exponer y gestionar servidores MCP (Model Context Protocol) a través del API Gateway:

```mermaid
graph TB
    subgraph MCP_ARCH ["🔌 MCP Gateway Architecture"]
        direction TB
        MCPEXT["MCPGatewayExtension<br/>Controller"]
        
        subgraph REGISTRATIONS ["MCPServerRegistration CRs"]
            REG1["k8s-mcp-server<br/>Kubernetes operations"]
            REG2["ocp-mcp-server<br/>OpenShift operations"]
        end

        subgraph GATEWAY ["Gateway API Layer"]
            GW2["Gateway<br/>(mcp-gateway)"]
            HR1["HTTPRoute<br/>k8s-mcp"]
            HR2["HTTPRoute<br/>ocp-mcp"]
        end

        subgraph CONSUMERS ["Consumers"]
            OLS2["OpenShift<br/>Lightspeed"]
            DEVLS["Developer Hub<br/>Lightspeed"]
            CUSTOM["Custom<br/>MCP Clients"]
        end
    end

    MCPEXT --> REG1
    MCPEXT --> REG2
    REG1 --> HR1
    REG2 --> HR2
    HR1 --> GW2
    HR2 --> GW2
    OLS2 --> GW2
    DEVLS --> GW2
    CUSTOM --> GW2

    style MCP_ARCH fill:#0d0d0d,color:#fff,stroke:#609926,stroke-width:2px
    style MCPEXT fill:#609926,color:#fff
    style REGISTRATIONS fill:#1a1a1a,color:#fff,stroke:#609926
    style GATEWAY fill:#1a1a1a,color:#fff,stroke:#0066CC
    style CONSUMERS fill:#1a1a1a,color:#fff,stroke:#EE0000
    style REG1 fill:#0066CC,color:#fff
    style REG2 fill:#0066CC,color:#fff
    style GW2 fill:#0066CC,color:#fff
    style HR1 fill:#4078c0,color:#fff
    style HR2 fill:#4078c0,color:#fff
    style OLS2 fill:#EE0000,color:#fff
    style DEVLS fill:#EE0000,color:#fff
    style CUSTOM fill:#151515,color:#fff
```

El flujo de MCP Gateway:

1. **`MCPGatewayExtension`** activa el controlador MCP en Kuadrant.
2. **`MCPServerRegistration`** CRs registran cada servidor MCP (k8s-mcp, ocp-mcp).
3. El controlador crea automáticamente `HTTPRoute`s que exponen los MCP servers a través del `Gateway`.
4. **OpenShift Lightspeed** y **Developer Hub Lightspeed** consumen los MCP servers via el Gateway, permitiendo a la IA ejecutar operaciones sobre Kubernetes y OpenShift.

## Connectivity Link: Gateway + Políticas + Modelos de Auth

```mermaid
graph TB
    subgraph CL ["🌐 Red Hat Connectivity Link"]
        direction TB
        
        subgraph OIDC_MODEL ["Modelo OIDC (Neuralbank)"]
            CLIENT1["🌍 Browser"] --> GW1["Gateway"]
            GW1 --> OIDCP["OIDCPolicy<br/>Keycloak redirect"]
            GW1 --> RLP1["RateLimitPolicy<br/>60 req/min/user"]
            GW1 --> HR_NB["HTTPRoute<br/>/api → backend"]
            HR_NB --> SVC1["neuralbank-backend"]
        end

        subgraph APIKEY_MODEL ["Modelo API Key (NFL Wallet)"]
            CLIENT2["🤖 Script M2M"] --> GW2B["Gateway"]
            GW2B --> AP["AuthPolicy<br/>X-API-Key header"]
            GW2B --> RLP2["RateLimitPolicy<br/>Global burst"]
            GW2B --> HR_NFL["HTTPRoute<br/>/api → backend"]
            HR_NFL --> SVC2["nfl-wallet-backend"]
        end
    end

    style CL fill:#0d0d0d,color:#fff,stroke:#0066CC,stroke-width:2px
    style OIDC_MODEL fill:#1a1a1a,color:#fff,stroke:#4078c0
    style APIKEY_MODEL fill:#1a1a1a,color:#fff,stroke:#ef7b4d
    style CLIENT1 fill:#151515,color:#fff
    style CLIENT2 fill:#151515,color:#fff
    style GW1 fill:#0066CC,color:#fff
    style GW2B fill:#0066CC,color:#fff
    style OIDCP fill:#4078c0,color:#fff
    style AP fill:#ef7b4d,color:#fff
    style RLP1 fill:#ef7b4d,color:#fff
    style RLP2 fill:#ef7b4d,color:#fff
    style HR_NB fill:#0066CC,color:#fff
    style HR_NFL fill:#0066CC,color:#fff
    style SVC1 fill:#EE0000,color:#fff
    style SVC2 fill:#EE0000,color:#fff
```

| Aspecto | Neuralbank (OIDC) | NFL Wallet (API Key) |
|---------|-------------------|---------------------|
| **Auth** | Token JWT (Bearer) via Keycloak | API Key estática (header) |
| **Flujo** | Redirect a login page | Sin redirect, key M2M |
| **Header** | `Authorization: Bearer <token>` | `X-API-Key: <key>` |
| **Rate limit** | Por usuario autenticado | Global (todas las keys) |

## Rol de cada componente

| Componente | Rol |
| --- | --- |
| Developer Hub | Portal del desarrollador: catálogo, plantillas, documentación, Lightspeed, notificaciones y plugins. |
| Console Links | Middleware de navegación en OpenShift Console con acceso directo a todas las herramientas. |
| Gitea | Repositorio Git interno: código fuente, manifiestos y triggers para pipelines. |
| Argo CD | Sincronización GitOps desde Git al clúster; salud y drift visibles en dashboard. |
| Tekton | Pipelines CI/CD como recursos de Kubernetes. Visible en pestaña **CI** del Hub. |
| Keycloak | Identidad y SSO; alimenta OIDCPolicy y acceso al Hub. |
| Dev Spaces | IDEs en el navegador conectados al mismo repo que GitOps y Tekton. |
| MCP Gateway (Kuadrant) | Extensión comunitaria que expone MCP servers via Gateway API para consumo por LLMs. |
| Connectivity Link | Gateway API + Istio + Kuadrant: entrada norte-sur, enrutamiento y políticas sobre APIs. |
| Lightspeed | Asistente IA en Developer Hub con RAG y conexión a LLM via LiteLLM. |
| OpenShift Lightspeed | Asistente IA en la consola OpenShift, conectado a MCP servers via MCP Gateway. |
| Notifications + Mailpit | Notificaciones in-app y email sobre el ciclo de vida de componentes. |
