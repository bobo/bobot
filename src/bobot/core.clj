(ns bobot.core
  (:use [irclj.core]
        [net.cgrand.enlive-html]
        [bobot.wolframalpha]
        )
  (:require [clj-http.client :as client])
  (:import (java.net URL URLConnection URLEncoder)))


(defn url-encode
  "Wrapper around java.net.URLEncoder returning a (UTF-8) URL encoded
representation of text."
  [text]
  (URLEncoder/encode text "UTF-8"))

(defn make-url [address]
  (println "address:" address)
    (if (.startsWith address "http")
      (URL. address)
      (URL. (str "http://" address))))

(defn get-title [url]
  (-> url
      make-url
      html-resource
      (select [:title])
      first :content first))



(defn say-title [irc channel message]
  (println "m:" message)
  (send-message irc channel
                (-> message
                    second
                    get-title)))

(defn cleanup [string]
  (->
   string
   (.replaceAll "Antal" "")
   (.replaceAll "anm.lda" "")))

(defn say-booked [irc channel]
  (send-message irc channel 
                (cleanup
                  (reduce  #(str %1 " \n " %2)
                           (filter #(.contains (str %) "Paticipants")
                                   (map first
                                        (map :content
                                             (-> "http://www.hazard.nu/nextlan_participants.asp" 
                                                 URL. 
                                                 html-resource 
                                                 (select [:p])))))))))


(def url-pattern  #".*((http|www)[^ ]*).*")

(defn say-is-prime [irc channel num]
  (send-message irc channel 
                (str  (.isProbablePrime (BigInteger. (second  num)) 42))))

(defn say-wolfram [irc channel question]  (send-message irc channel (get-answer (url-encode (second  question)))))


(defn on-message  [{:keys [channel message irc nick]}]
  (condp re-matches message
    url-pattern :>> (partial say-title irc channel)
    #"prime: ([\d]*)" :>> (partial say-is-prime irc channel)
    #"wolfram:[ ]*(.*)" :>> (partial say-wolfram irc channel)
    #"w:[ ]*(.*)" :>> (partial say-wolfram irc channel)
    #"bokade.*" (say-booked irc channel)
    nil)) 




