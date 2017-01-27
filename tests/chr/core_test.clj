(ns chr.core-test
  (:require [clojure.test :refer :all]
            [chr.core :as c]))

(deftest changes-test
  (testing "flat map"
    (let [v1 {:_id 1 :first-name "Bruce" :last-name "Norries"}
          v2 {:_id 1 :first-name "Bruces" :last-name "Willis"}]
      (is (= [{:field :first-name :old "Bruce" :new "Bruces"}
              {:field :last-name :old "Norries" :new "Willis"}]
             (c/changes v1 v2)))))
  (testing "nested map"
    (let [v1 {:_id 1 :name "Bruce Norries" :address {:street "Some street"}}
          v2 {:_id 1 :name "Bruce Willis" :address {:street "Nakatomi Plaza"}}]
      (is (= [{:field :name, :old "Bruce Norries", :new "Bruce Willis"},
              {:field :address.street, :old "Some street", :new "Nakatomi Plaza"}]
             (c/changes v1 v2))))))

(deftest flatten-data-test
  (testing "Flat"
    (let [tuples [[:a "a" :b "b"]]]
      (is (= [[:a "a" :b "b"]]
             (c/flatten-data tuples)))))
  (testing "One level nesting"
    (let [tuple [[:a {:b "a" :c "b"} {:b "A" :c "B"}]]]
      (is (= [[:a.b "a" "A"] [:a.c "b" "B"]]
             (c/flatten-data tuple)))))
  (testing "Two level nesting"
    (let [tuple [[:a {:b "a" :c {:d "d"} :e 1} {:b "A" :c {:d "D"} :e 2}]]]
      (is (= [[:a.b "a" "A"] [:a.c.d "d" "D"] [:a.e 1 2]]
             (c/flatten-data tuple))))))
