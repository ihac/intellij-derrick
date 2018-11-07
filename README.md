# intellij-derrick
An IntelliJ IDEA plugin which integrates [Derrick](https://alibaba.github.io/derrick/) and supports automated Kubernetes deployment.

## Features

- Automatically detect application source code and generate Dockerfile, Kubernetes-Yaml by utilizing Derrick.
- Build Docker image from Dockerfile and run as test container.
- Push Docker image to remote registry.
- Deploy application to remote Kubernetes cluster.
- Support Aliyun Registry and Aliyun Container Service.

## Prerequisites

You will need:

- [derrick](https://github.com/alibaba/derrick) installed (currently derrick is not compatible with our plugin, but it won't take long).
- (at least) an account of Docker registry (DockerHub or any third-party registries that support password authentication).
- (at least) an account of Kubernetes cluster (i.e. `kubeconfig`) or access key of Alicloud Container Service.

## Installation

You can install this plugin in two different ways:

1. Install directly from IDE:

   Open `Perferences` dialog, and then go to `Plugins`; Click the `Browse repositories...` button; Search `derrick` and install; Restart IDE.

2. Download from Github:

   Click `Releases`; Select and download the plugin file in required version; Install the plugin from disk (refer to [official docs](https://www.jetbrains.com/help/idea/installing-plugin-from-disk.html)).

## User Guide

**Note:** Please make sure the toolbar is visible by checking `View->Toolbar`.

Users should configure the plugin first before using it: Open Settings/Preferences dialog, and click `Derrick` in the Tools group. Set the path to derrick executable file and add your own Docker registries and Kubernetes clusters.

There are 4 main actions provided by this plugin: Init, Serve, Push and Deploy.

- Init: Automatically detect application source code and generate Dockerfile, Kubernetes-Yaml by leveraging derrick.
- Serve: Build Docker image from Dockerfile and run as container.
- Push: Push Docker image to remote registry.
- Deploy: Deploy application to remote Kubernetes cluster.

Users are able to select one of these actions by clicking the derrick icon in IntelliJ IDEA toolbar.

## Plugin Versioning Specs

Refer to [Semantic Versioning 2.0.0](https://semver.org/)
