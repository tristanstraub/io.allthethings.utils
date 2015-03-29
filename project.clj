(defproject io.allthethings.utils "0.1.7-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2816" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [org.clojure/core.cache "0.6.3"]
                 [org.clojure/core.memoize "0.5.6" :exclusions [org.clojure/core.cache]]
                 [hiccup "1.0.5"]

                 [http-kit "2.1.19"]

                 [compojure "1.3.1" :exclusions [joda-time]]
                 [com.taoensso/carmine "2.9.1"]]

  :min-lein-version "2.5.0"

  :plugins [[lein-cljsbuild "1.0.3"]]

  :test-paths ["test/clj"]
  :source-paths ["src/clj"]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/classes"
                   :rules :cljs}
                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :clj}
                  {:source-paths ["test/cljx"]
                   :output-path "target/test-classes"
                   :rules :cljs}]}

  :prep-tasks [["cljx" "once"] "javac" "compile"]
  :hooks [leiningen.cljsbuild]

  :cljsbuild {:builds [{:id "allthethings"
                        :source-paths ["target/classes"]
                        :compiler {:output-to "target/allthethings.js"
                                   :optimizations :simple
                                   ;;:target :nodejs
                                   :pretty-print true}}]}

  :profiles {:dev {:repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
                                                     cljx.repl-middleware/wrap-cljx]}

                   :cljsbuild {:builds [{:source-paths
                                         ["target/classes" "test/cljs"]
                                         :compiler {:output-to     "resources/public/js/app.js"
                                                    :output-dir    "resources/public/js/out"
                                                    :source-map    "resources/public/js/out.js.map"

                                                    :optimizations :simple
                                                    :pretty-print true}}]}

                   :test-paths ["target/test-classes"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [com.keminglabs/cljx "0.6.0"]
                                  [enlive "1.1.5"]
                                  [midje "1.6.3"]
                                  [clj-webdriver "0.6.1"]]

                   :source-paths ["target/classes"]
                   :plugins [[com.cemerick/clojurescript.test "0.3.1"]
                             [lein-cljsbuild "1.0.4-SNAPSHOT"]
                             [com.keminglabs/cljx "0.6.0" :exclusions [org.clojure/clojure]]]}})
