(ns bobot.filelogging
  (:use [clojure.contrib.duck-streams :only (append-spit)])
  (:require [net.cgrand.enlive-html :as html])
  (:require clojure.contrib.string)
  (:import java.util.Date))

(defn add-line [logline file]
  (append-spit file (str logline "\n")))
    
(defn create-log-line [message sender]
  (str (Date.) "<" sender "> " message ))


(def *my-sel* [:tr#row])

(html/defsnippet log-model 
  "bobot/log.html"
  [:table#log]
  [ctxt]
  [:tr#row] (html/clone-for [log ctxt]
                            [:td] (html/content log)))
  
   
(html/deftemplate index "bobot/log.html" [ctxt]
  [:h1#channel_name] (html/content (:channel ctxt))
  [:table#log] (html/content (log-model (:log ctxt))))

(defn log-list [filename]
  (map str (.split (slurp (str filename ".txt")) "\n")))


;(defn log-line [message sender channel]
;  (let [filename (.replace channel "#" "")]
;    (add-line (create-log-line message sender) (str filename ".txt"))
;    (spit (str "/home/bobo/public_html/" filename ".html" )
;          (apply str (index {
;                             :log (log-list filename)
;                             :channel channel
;                             }
;                            )))))

(def *base-dir* "/home/bobo/public_html/")

(defn html-log [filename channel]
  (spit (str *base-dir* filename ".html" )
        (apply str (index {
                           :log (log-list filename)
                           :channel channel
                           }
                          ))))

  

(defn log-line [message sender channel]
  (let [filename (.replace channel "#" "")]
    (add-line (create-log-line message sender) (str filename ".txt"))
    (html-log filename channel)))
