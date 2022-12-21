import time
from locust import HttpUser, task, between

class LoadTest(HttpUser):

    @task
    def customer_with_less_orders_v2(self):
        self.client.get("/v2/customer/1/totalOrderAmount")

    @task
    def customer_with_more_orders_v2(self):
        self.client.get("/v2/customer/2/totalOrderAmount")

    @task
    def customer_with_most_orders_v2(self):
        self.client.get("/v2/customer/3/totalOrderAmount")

    @task
    def customer_with_less_orders_v1(self):
        self.client.get("/v1/customer/1/totalOrderAmount")

    @task
    def customer_with_more_orders_v1(self):
        self.client.get("/v1/customer/2/totalOrderAmount")

    @task
    def customer_with_most_orders_v1(self):
        self.client.get("/v1/customer/3/totalOrderAmount")

