from locust import HttpUser, task, between
import random

class OrderServiceUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def should_get_order_by_id(self):
        order_id = random.randint(1, 4)
        path = f"/order-service/api/orders/{order_id}"

        with self.client.get(path, catch_response=True, name="/api/orders/{id}") as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")
