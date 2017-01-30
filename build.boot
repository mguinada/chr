(set-env!
 :source-paths  #{"src" "tests"}
 :test-paths    #{"tests"}
 :dependencies '[[adzerk/boot-reload "0.4.12" :scope "test"]
                 [adzerk/boot-test "1.1.2" :scope "test"]
                 [metosin/boot-alt-test "0.2.1" :scope "test"]
                 [org.clojure/tools.namespace "0.2.11" :scope "test"]
                 [congomongo "0.5.0"]
                 [compojure "1.5.2"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring "1.5.1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.2.1"]
                 [cheshire "5.7.0"]])

(require
 '[boot.task.built-in    :refer [aot]]
 '[adzerk.boot-reload    :refer [reload]]
 '[adzerk.boot-test      :refer [test]]
 '[metosin.boot-alt-test :refer [alt-test]]
 '[chr.web.service       :refer [-main]])

;;clojure namespace tools integration
(swap! boot.repl/*default-dependencies* conj
      '[org.clojure/tools.namespace "0.2.11"])

;; CIDER integration
(swap! boot.repl/*default-dependencies*
       concat '[[cider/cider-nrepl "0.14.0"]
                [refactor-nrepl "2.2.0"]])

(swap! boot.repl/*default-middleware*
       conj 'cider.nrepl/cider-middleware)

;; Tasks

(deftask build []
  (comp (speak)
        (aot)))

(deftask run []
  (comp (watch)
        (repl)
        (reload)
        (build)))

(deftask run-server []
  (comp (build)
        (repl)
        (with-pre-wrap fileset (-main) fileset)))

(deftask run-tests
  [a autotest bool "If no exception should be thrown when tests fail"]
  (comp
   (alt-test :fail (not autotest))))

(deftask autotest []
  (comp
   (watch)
   (run-tests :autotest true)))

(deftask development []
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))
