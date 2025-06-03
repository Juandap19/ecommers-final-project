from locust import HttpUser, task, between
import random

class PaymentServiceUser(HttpUser):
    wait_time = between(1, 3)
    payment_ids = [1, 2, 3]
    order_ids = [1, 2, 3, 4]

    @task
    def get_all_favourites(self):
        path = "/payment-service/api/payments"
        with self.client.get(path, catch_response=True, name="/api/payments") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")


    @task
    def get_payment_by_id(self):
        payment_id = random.choice(self.payment_ids)
        path = f"/payment-service/api/payments/{payment_id}"
        with self.client.get(path, catch_response=True, name="/api/payments/{id}") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")


    def get_order_id(self):
        return random.choice(self.order_ids)

    @task
    def should_save_payment(self):
        order_id = self.get_order_id()
        order_reference_payload = {
            "orderId": order_id
        }

        payment_payload = {
            "isPayed": True,
            "paymentStatus": "COMPLETED",
            "order": order_reference_payload
        }

        with self.client.post(
                "/payment-service/api/payments",
                json=payment_payload,
                catch_response=True,
                name="/api/payments"
        ) as response:
            if 200 <= response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")