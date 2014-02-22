(ns limn.history
  (:require [goog.events :as evt]
            [goog.history.Html5History]))

;(deftype PizzaTokenTransformer
;  Object
;  (createUrl [this] 5))
;
;(set! (.-createUrl (.-prototype PizzaTokenTransformer))
;      (fn [token pathPrefix location]
;        (.-foo )))
;
;(set! (.-retrieveToken (.-prototype PizzaTokenTransformer))
;      (fn [token pathPrefix location] nil))
;
;(.createUrl (PizzaTokenTransformer.))

;(.keys js/Object #js {:fooX 1})
