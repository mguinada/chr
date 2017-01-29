(ns chr.service-test
  (:require [clojure.test :refer :all]
            [chr.persistence :as p]
            [chr.service :as s]))

(defn fixture
  [f]
  (p/connect "challenge_test")
  (p/drop-coll! :users)
  (p/drop-coll! :user-changes)
  (f))

(use-fixtures :each fixture)

(deftest create-test
  (let [v1 {:name "Bruce Norries" :address {:street "Some street"}}
        v2 {:name "Bruce Willis" :address {:street "Nakatomi Plaza"}}]
    (testing "Create"
      (is (zero? (p/count :users)))
      (s/save! v1)
      (is (= 1 (p/count :users))))))

(deftest update-test
  (let [v1 {:name "Bruce Norries" :address {:street "Some street"}}
        v2 {:name "Bruce Willis" :address {:street "Nakatomi Plaza"}}]
    (testing "Update"
      (let [user (s/save! v1)]
        (is (= 1 (p/count :users)))
        (is (zero? (p/count :user-changes)))
        (s/save! (assoc v2 :_id (:_id user)))
        (is (= 1 (p/count :users)))
        (is (= 2 (p/count :user-changes)))))))
