[![Build](https://img.shields.io/github/actions/workflow/status/ProvideQ/toolbox-server/deploy-main.yml?style=for-the-badge)](https://github.com/ProvideQ/toolbox-server/actions/workflows/deploy-main.yml)
[![Release](https://img.shields.io/github/v/release/ProvideQ/toolbox-server?style=for-the-badge)](https://github.com/ProvideQ/toolbox-server/releases/)

# ProvideQ Toolbox Server
This repository contains the source code for the [ProvideQ](https://provideq.org) Toolbox server.
A web-based user interface can be found in our
[ProvideQ/ProvideQ repository](https://github.com/ProvideQ/ProvideQ).

## Development setup
1. Install Java 17 (check with `java -version`)
2. Clone this repository
3. Install a python env that works with GAMS (skip this step if you don't need GAMS)
   1. Install GAMS.
      * Make sure the `gams` / `gams.exe` executable is available on the path!
   2. Install miniconda (or anaconda, if you prefer that):
      https://conda.io/projects/conda/en/stable/user-guide/install/index.html
   3. Create a GAMS conda environment: `conda create --name gams python=3.10 --yes`
   4. Activate your conda environment: `conda activate gams`.
   5. Make GAMS use that python environment by setting the `GMSPYTHONLIB`
      environment variable.
      * Linux: `GMSPYTHONLIB=<path-to-conda>/envs/gams/lib/libpython3.10.so`
      * Windows: `GMSPYTHONLIB=<path-to-conda>\envs\gams\python310.dll`
   6. Install GAMS packages to the GAMS conda env:
      * Linux: `pip install gams[core,connect] --find-links <path-to-gams>/api/python/bdist`
         * If you get an error building `psycopg2`, try to install these postgres packages:
           `sudo apt-get install postgresql libpq-dev` and run the `pip install ...` command again
      * Windows: `pip install gams[core,connect] --find-links <path-to-gams>\api\python\bdist`
4. Install solver dependencies (skip this step if you don't want to use the solvers):
   * Note that these dependencies must be installed to the gams conda env if you want to use GAMS and other solvers from
     the same toolbox installation!
   1. Install GAMS solver dependencies: `pip install -r gams/requirements.txt`
   2. Install Qiskit solver dependencies: `pip install -r qiskit/requirements.txt` 
5. Run the server using `./gradlew bootRun`

## Deployment
This repository is designed to be deployed with [Dokku](https://dokku.com/) but you can also run 
the Java application directly or inside a docker container (`Dockerfile` is included!).
The docker container can be built and run as follows:
```shell
docker build --tag provideq-toolbox-backend --build-arg GAMS_LICENSE=$(base64 -w 0 ./gamslice.txt) .
docker run --publish 8080:5000 provideq-toolbox-backend
```

## Releasing a new version
1. Create a release branch from develop: `git checkout -b release/x.y.z`.
2. Bump the version number in the `build.gradle` file to the new version number and commit it to the release branch.
3. Push to GitHub and create a pull request to merge the release branch into `main`.
4. Make sure to test your new version!
5. Write a changelog.
   The PR can help you identify differences between the last release (`main`) and the next one (your release branch).
6. Merge the PR into main.
7. [Create a new GitHub release](https://github.com/ProvideQ/toolbox-server/releases/new) with a new tag named like your
   version number `x.y.z` and use the changelog as the description.
8. Pull the main branch (`git checkout main && git pull`),
   merge it into the develop branch (`git checkout develop && git pull && git merge main`)
   and push it (`git push`).

## License
Copyright (c) 2022 - 2023 ProvideQ

This project is available under the [MIT License](./LICENSE).