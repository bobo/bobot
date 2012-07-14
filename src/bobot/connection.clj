(ns bobot.connection
  (:use [irclj.core]
        bobot.core))


(def irc
  (create-irc {:name "bobot21",
               :username "bobot21"
               :server "irc.se.quakenet.org",
               :auto-reconnect-delay-mins 1

               :fnmap {:on-message #'on-message}}))


(defonce bot
  (connect irc
           :channels ["#bobotestar" "#bawbot"]
           :in-encoding "UTF-8"
           :out-encoding "UTF-8"))
