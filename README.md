# Build Environment

This repository contains the configuration files used for building and testing
code at MousePaw Media. These are primarily intended to be used with our
Jenkins and Docker setup.

## Building Docker Images

To build and publish a Docker image to our private registry, first
[install Docker and log into the registry](https://devdocs.mousepawmedia.com/tools/docker.html).
Then, run the following from the root of your repository, changing the name of the image from
`mpm-jammy` to whatever you want.

```
docker image build dockerfiles/mpm-jammy
# Note the image hash provided at the end of the last command, and use it below in place of XXXXXXXXXXX
docker tag XXXXXXXXXXX registry.mousepawmedia.com/mpm-jammy:latest
docker push registry.mousepawmedia.com/mpm-jammy:latest
```

If you are building both `mpm-*` and `jenkins.mpm-*`, be sure to AT LEAST build and tag (if not push)
the `mpm-*` image before building the corresponding `jenkins.mpm-*` image, or the latter will pull
the older version of `mpm-*` from the remote registry.

## Authors

Jason C. McDonald

## Contributions

We do NOT accept pull requests through GitHub.
If you would like to contribute code, please read our
[Contribution Guide](https://www.mousepawmedia.com/developers/contribution).

All contributions are licensed to us under the
[MousePaw Media Terms of Development](https://www.mousepawmedia.com/termsofdevelopment).

## License

All files in this repository are licensed under the BSD-3 License, unless
otherwise indicated. (See LICENSE.md)

The project is owned and maintained by [MousePaw Media](https://www.mousepawmedia.com/developers).
