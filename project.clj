(defproject bobot "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dependencies [[org.clojure/clojure "1.4.0"]              
		 [irclj "0.4.0-SNAPSHOT"]
                 [enlive "1.0.0-SNAPSHOT"]               
                 [midje "1.1-alpha-1"]
                 [clj-http "0.1.2"]
                 [org.clojure/data.json "0.1.2"]
		 ]
  :plugins [[lein-swank "1.4.4"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :main bobot.core)
  

