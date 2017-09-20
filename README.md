# rss-pipe

Read your github activity feed via rss and allows to filter / reduce it

[![Build Status][BS img]][Build Status]

## Install

```sh
git clone git@github.com:clojure-ru/rss-feed.git
cp dev-resources/config.edn.sample dev-resources/config.edn 
```

Get the link from "Subscribe to your news feed" on https://github.com and edit the config file

## Usage

```sh
lein repl
(reset)
```

Also see helpful examples in comment section of `dev/dev.clj`

## Development

Please install this git hook:

```sh
echo 'lein lint' > .git/hooks/pre-push
chmod +x .git/hooks/pre-push
```

## License

Copyright © 2017 Vlad Bokov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[Build Status]: https://travis-ci.org/clojure-ru/rss-feed
[BS img]: https://travis-ci.org/clojure-ru/rss-feed.png
