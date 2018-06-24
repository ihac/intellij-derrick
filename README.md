# intellij-derrick
An IntelliJ IDEA plugin which integrates [Derrick](https://alibaba.github.io/derrick/) and supports automated Kubernetes deployment.

## Prerequisites

You will need:  

- [derrick](https://github.com/alibaba/derrick) installed (currently derrick is not compatible with our plugin, but it won't take long).  
- (at least) an account of Docker registry (Docker or any third-party registries that support password authentication).  
- (at least) an account of Kubernetes cluster (i.e. `kubeconfig`) or access key of Alicloud Container Service.  

## Installation

You can install this plugin in two different ways:  

1. Install directly from IDE:

   Open `Perferences` dialog, and then go to `Plugins`; Click the `Browse repositories...` button; Search `derrick` and install; Restart IDE.

2. Download from Github: 

   Click `Releases`; Select and download the plugin file in required version; Install the plugin from disk (refer to [official docs](https://www.jetbrains.com/help/idea/installing-plugin-from-disk.html)).

## Plugin Versioning Specs

Refer to [Semantic Versioning 2.0.0](https://semver.org/)
