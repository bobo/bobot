(ns bobot.core
  (:use [irclj.core]
        [net.cgrand.enlive-html]
        [bobot.filelogging])
  (:require [clj-http.client :as client])
  (:import (java.net URL URLConnection URLEncoder)))


(defn url-encode
  "Wrapper around java.net.URLEncoder returning a (UTF-8) URL encoded
representation of text."
  [text]
  (URLEncoder/encode text "UTF-8"))

(defn get-title [url]
  (-> url
      html-resource
      (select [:title])
      first :content first))

(defn do-search [query]
  (let [google (.openConnection (URL. (str "https://www.google.com/search?as_q=" query "&hl=sv")))]
    (.setRequestProperty google "User-agent" "irclj-bobot")
    (let [ url (->
                (select (html-resource
                         (.getInputStream google)) [:a.l])
                first
                :attrs
                :href
                )]
      (str (-> url
               make-url
               get-title)
           " " url
           ))))

(defn cleanup [page]
  (flatten (map #(if (map? %) (:content %) %) page)))

(defn get-info [query]
  (let [google (.openConnection (URL. (str "https://www.google.com/search?as_q=" query "&hl=sv")))]
    (.setRequestProperty google "User-agent" "irclj-bobot")
    (apply str (->
                (select (html-resource
                         (.getInputStream google)) [:div.s])
                first
                :content
                cleanup
                cleanup
                cleanup
                
                ))))

(defn search-command [irc channel message]
  (send-message irc channel
                (->  message
                     second
                     url-encode
                     do-search)))
(defn search-command-info [irc channel message]
  (send-message irc channel
                (->  message
                     second
                     url-encode
                     get-info)))

(defn make-url [address]
    (println address)
    (if (.startsWith address "http")
      (URL. address)
      (URL. (str "http://" address))))



(defn say-title [irc channel message]
  (do (println  message)
      (send-message irc channel
                    (-> message
                        first
                        make-url
                        get-title))))

(defn cleanup [string]
  (->
   string
   (.replaceAll "Antal" "")
   (.replaceAll "anm.lda" "")))

(defn say-booked [irc channel]
  (send-message irc channel 
                (cleanup
                  (reduce  #(str %1 " \n " %2)
                           (filter #(.contains (str %) "Antal")
                                   (map first
                                        (map :content
                                             (-> "http://www.hazard.nu/nextlan_anm.asp" 
                                                 URL. 
                                                 html-resource 
                                                 (select [:p])))))))))


(def url-pattern  #"(http://[^ ]*)||(www.[^ ]*).*")

(defn on-message  [{:keys [channel message irc nick]}]
  (condp re-matches message
    url-pattern :>> (partial say-title irc channel)
    #"google:[ ]*(.*)" :>> (partial search-command irc channel)
    #"info:[ ]*(.*)" :>> (partial search-command-info irc channel)
    #"bokade.*" (say-booked irc channel)
    nil)
  (log-line message nick channel)
  )



(def irc
  (create-irc {:name "bobot2",
               :username "bobot2"
               :server "irc.se.quakenet.org",
               :auto-reconnect-delay-mins 1
               :fnmap {:on-message #'on-message}}))


(defonce bot2
  (connect irc
           :channels ["#bobotestar" "#bawbot"]))
