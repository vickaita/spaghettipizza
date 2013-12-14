(ns vickaita.channels
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [goog.net.XhrManager]
            [goog.net.WebSocket]
            [goog.Uri]
            [goog.Uri.QueryData :as QD]
            [goog.dom.forms :as forms]
            [goog.events :as events]
            [cljs.core.async :as async :refer [put! take! close! chan <! map<]]
            [cljs.core.async.impl.protocols :as impl]
            [vickaita.console :refer [log]]))

(def ^{:dynamic true :private true} *xhr-manager* (goog.net.XhrManager. 0))
(def ^:private id-counter (atom 0))

(defn- id []
  (let [i @id-counter]
    (swap! id-counter inc)
    (str "request-" i)))

(defn request
  ([url] (request url "GET" nil))
  ([url method] (request url method nil))
  ([url method params]
   (log params)
   (let [out (chan)]
     (.send *xhr-manager*
            (id)
            (goog.Uri. url)
            method
            params
            (js-obj "Accept" "application/json")
            100
            (fn [res] (put! out res)))
     out)))

(defn request-form
  [form]
  (request (.getAttribute form "action")
           (.getAttribute form "method")
           (forms/getFormDataString form)))


(defn ajax-form-channel
  [form]
  (evt/listen form "submit" (fn [e]
                              (.preventDefault e)
                              (let [req (ajax/request-form form)]
                                (go (let [res (<! req)]
                                      (.log js/console res)))))))

(defn event
  [event-type element]
  (let [out (chan)]
    (evt/listen element event-type #(put! out %))
    out))

(defn websocket
  [url]
  (let [sock (goog.net.WebSocket.)
        incoming (chan 100)
        outgoing (chan 100)]
    (events/listen sock (array goog.net.WebSocket.EventType.OPENED
                               goog.net.WebSocket.EventType.CLOSED
                               goog.net.WebSocket.EventType.ERROR
                               goog.net.WebSocket.EventType.MESSAGE)
                   #(put! incoming %))
    (events/listen sock goog.net.WebSocket.EventType.OPENED
                   #(go-loop [msg (<! outgoing)]
                          (if (.isOpen sock)
                            (do (.log js/console "send" msg)
                                (.send sock msg)
                                (recur (<! outgoing)))
                            (do (.log js/console "not open")
                                (close! incoming)))))
    (.open sock url)
    (reify
      impl/ReadPort
      (take! [_ fn1] (impl/take! incoming fn1))
      impl/WritePort
      (put! [_ val fn0] (impl/put! outgoing val fn0))
      impl/Channel
      (close! [_] (do (doto sock (.close) (.removeAllListeners))
                      (impl/close! outgoing)
                      (impl/close! incoming))))))
