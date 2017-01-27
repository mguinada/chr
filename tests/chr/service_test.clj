(ns chr.service-test
  (:require [clojure.test :refer :all]
            [chr.service :as s]))

(deftest changes-test
  (let [v1 {:_id 1 :name "Bruce Norries" :address {:street "Some street"}}
        v2 {:_id 1 :name "Bruce Willis" :address {:street "Nakatomi Plaza"}}]
    (is (= [{:field "name", :old "Bruce Norris", :new "Bruce Willis"},
            {:field "address.street", :old "Some Street", :new "Nakatomi Plaza"}]
           (s/changes v1 v2)))))
