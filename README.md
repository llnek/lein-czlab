# lein-czlab

A Leiningen plugin (hooks) to extend leiningen's build process.
This plugin is meant to be used on `czlab` projects.

## Usage

Include the plugin in the :plugins vector in project.clj:

:plugins [[lein-czlab "0.1.1"]]

To activate the hooks:

:hooks [leiningen.lein-czlab]

## License

Copyright © 2013-2017 Kenneth Leung

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
