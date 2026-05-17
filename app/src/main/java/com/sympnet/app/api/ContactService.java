package com.sympnet.app.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ContactService {
    class ContactMessageRequest {
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
        public String topic;
        public String message;
        
        public ContactMessageRequest(String firstName, String lastName, String email, String phone, String topic, String message) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.topic = topic;
            this.message = message;
        }
    }

    class ContactResponse {
        public String message;
    }

    @POST("api/ContactMessages")
    Call<ContactResponse> createContactMessage(@Body ContactMessageRequest request);
}
