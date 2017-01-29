(ns chr.service-test
  (:require [clojure.test :refer :all]
            [chr.core :as c]
            [chr.persistence :as p]
            [chr.service :as s]))

(defn fixture
  [f]
  (p/connect "challenge_test")
  (f)
  (p/drop-coll! :users)
  (p/drop-coll! :user-changes))

(use-fixtures :each fixture)

(deftest create-test
  (let [v1 {:name "Bruce Norries" :address {:street "Some street"}}
        v2 {:name "Bruce Willis" :address {:street "Nakatomi Plaza"}}]
    (testing "Create"
      (is (zero? (p/fetch-count :users)))
      (s/save! v1)
      (is (= 1 (p/fetch-count :users))))))

(deftest update-test
  (let [v1 {:name "Bruce Norries" :address {:street "Some street"}}
        v2 {:name "Bruce Willis" :address {:street "Nakatomi Plaza"}}]
    (testing "Update"
      (let [user (s/save! v1)
            id (:_id user)]
        (is (= 1 (p/fetch-count :users)))
        (is (zero? (p/fetch-count :user-changes)))
        (is (= {:_id id :name "Bruce Norries" :address {:street "Some street"}}
               (s/fetch id)))
        (s/save! (assoc v2 :_id (:_id user)))
        (is (= 1 (p/fetch-count :users)))
        (is (= 2 (p/fetch-count :user-changes)))
        (is (= {:_id id :name "Bruce Willis" :address {:street "Nakatomi Plaza"}}
               (s/fetch id)))))))

(deftest changes-test
  (let [v1 {:name "Bruce Norries" :address
            {:street "Some street" :zipcode {:prefix "000" :code "000"}}}
        v2 {:name "Bruce Willis" :address
            {:street "Nakatomi Plaza" :zipcode {:prefix "321" :code "654"}}}
        v3 {:name "Mr. Bruce Willis" :address
            {:street "Nakatomi Plaza" :zipcode {:prefix "111" :code "654XYZ"}}}
        t0 (c/timestamp)
        user-v1 (s/save! v1)
        _ (Thread/sleep 100)
        t1 (c/timestamp)
        user-v2 (s/save! (assoc v2 :_id (:_id user-v1)))
        _ (Thread/sleep 100)
        t2 (c/timestamp)
        user-v3 (s/save! (assoc v3 :_id (:_id user-v1)))
        _ (Thread/sleep 100)
        t3 (c/timestamp)]
    (testing "between t0 and t2"
      (is (= [{:field "address.street", :new "Nakatomi Plaza", :old "Some street"}
              {:field "address.zipcode.prefix", :new "321", :old "000"}
              {:field "address.zipcode.code", :new "654", :old "000"}
              {:field "name", :new "Bruce Willis", :old "Bruce Norries"}]
             (s/changes (:_id user-v1) t0 t2))))
    (testing "full timespan"
      (is (= [{:field "address.street", :new "Nakatomi Plaza", :old "Some street"}
              {:field "address.zipcode.prefix", :new "111", :old "000"}
              {:field "address.zipcode.code", :new "654XYZ", :old "000"}
              {:field "name", :new "Mr. Bruce Willis", :old "Bruce Norries"}]
             (s/changes (:_id user-v1) t0 t3))))))
