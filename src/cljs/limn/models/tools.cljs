(ns limn.models.tools)

(def ^:private groups
  [{:name "Pasta"
    :tools [{:id :spaghetti :name "Spaghetti"}
            {:id :linguini :name "Linguini"}
            ;{:id :path :name "Path"}
            {:id :fettuccine :name "Fettuccine"}
            {:id :lasagne :name "Lasagne"}
            {:id :ziti :name "Ziti"}]}
   {:name "Cheese"
    :tools [{:id :ricotta :name "Ricotta"}]}
   {:name "Herbs"
    :tools [{:id :basil :name "Basil"}]}])

(def toolbar {:groups groups})

(def default-tool (-> groups first :tools first))
