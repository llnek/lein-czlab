# lein-czlab

A Leiningen plugin (hooks) to extend leiningen's build process.
This plugin is meant to be used on `czlab` projects.

[![Build Status](https://travis-ci.org/llnek/lein-czlab.svg?branch=master)](https://travis-ci.org/llnek/lein-czlab)

## Installation

Add the following dependency to your `project.clj` file:

    [io.czlab/lein-czlab "1.0.0"]

## Documentation

* [API Docs](https://llnek.github.io/lein-czlab/)

## Usage

Include the plugin in the :plugins vector in project.clj:

:plugins [[lein-czlab "1.0.0"]]

To activate the hooks:

:hooks [leiningen.lein-czlab]

## Contacting me / contributions

Please use the project's [GitHub issues page] for all questions, ideas, etc. **Pull requests welcome**. See the project's [GitHub contributors page] for a list of contributors.

## License

Copyright © 2013-2017 Kenneth Leung

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

<!--- links (repos) -->
[CHANGELOG]: https://github.com/llnek/lein-czlab/releases
[GitHub issues page]: https://github.com/llnek/lein-czlab/issues
[GitHub contributors page]: https://github.com/llnek/lein-czlab/graphs/contributors






