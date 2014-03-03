(ns limn.models.colors)

(def ^:private palettes
  [{:name "Default Colors"
    :colors [{:name "Red" :fill "#F86969" :stroke "#F04F4F"}
             {:name "Orange" :fill "#EF951B" :stroke "#B55F11"}
             {:name "Yellow" :fill "#FAE265" :stroke "#DDAB0B"}
             {:name "Green" :fill "#83D874" :stroke "#5FBA52"}
             {:name "Blue" :fill "#6EB4E1" :stroke "#1E6B92"}
             {:name "Purple" :fill "#D459A4" :stroke "#AE4173"}
             {:name "White" :fill "#F2F2F2" :stroke "#D9D9D9"}
             {:name "Black" :fill "#202020" :stroke "#181818"}]}])

(def color-state {:palettes palettes})

(def default-color (-> palettes first :colors first))
