from locust import HttpUser, task, between

class FavouriteServiceUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def get_all_favourites(self):
        path = "/favourite-service/api/favourites"
        with self.client.get(path, catch_response=True, name="/api/favourites") as response:
            if response.status_code >= 200 and response.status_code < 300:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")