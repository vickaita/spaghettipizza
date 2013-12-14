(ns pizza.ajax
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [goog.net.XhrManager]
            [goog.Uri]
            [goog.Uri.QueryData :as QD]
            [goog.dom.forms :as forms]
            [cljs.core.async :refer [put! chan <! map<]]))

(def ^:private xhr-manager (goog.net.XhrManager. 0))
(def ^:private id-counter (atom 0))

(defn- id []
  (let [i @id-counter]
    (swap! id-counter inc)
    (str "request-" i)))

#_(defn- map->js-obj
  "Converts a Clojure map into a JavaScript object. Assumes that all keys in the
  map are keywords or strings."
  [m]
  (doall (reduce (fn [obj [k v]] (aset obj (name k) v)) (js-obj) m)))

(defn request
  ([url] (request url "GET" nil))
  ([url method] (request url method nil))
  ([url method params]
   (.log js/console params)
   (let [out (chan)]
     (.send xhr-manager
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
