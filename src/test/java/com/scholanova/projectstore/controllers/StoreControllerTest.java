package com.scholanova.projectstore.controllers;

import com.scholanova.projectstore.exceptions.ModelNotFoundException;
import com.scholanova.projectstore.exceptions.StoreNameCannotBeEmptyException;
import com.scholanova.projectstore.models.Store;
import com.scholanova.projectstore.services.StoreService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StoreControllerTest {

    @LocalServerPort
    private int port;

    private TestRestTemplate template = new TestRestTemplate();

    @MockBean
    private StoreService storeService;

    @Captor
    ArgumentCaptor<Store> createStoreArgumentCaptor;

    @Captor
    ArgumentCaptor<Integer> idArgumentCaptor;

    @Nested
    class Test_createStore {

        @Test
        void givenCorrectBody_whenCalled_createsStore() throws Exception {
            // Given
            String url = "http://localhost:{port}/stores";

            Map<String, String> urlVariables = new HashMap<>();
            urlVariables.put("port", String.valueOf(port));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{" +
                    "\"name\":\"Boulangerie\"" +
                    "}";
            HttpEntity<String> httpEntity = new HttpEntity<>(requestJson, headers);

            Store createdStore = new Store(123, "Boulangerie");
            when(storeService.create(createStoreArgumentCaptor.capture())).thenReturn(createdStore);

            // When
            ResponseEntity responseEntity = template.exchange(url,
                    HttpMethod.POST,
                    httpEntity,
                    String.class,
                    urlVariables);

            // Then
            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody()).isEqualTo(
                    "{" +
                            "\"id\":123," +
                            "\"name\":\"Boulangerie\"" +
                            "}"
            );
            Store storeToCreate = createStoreArgumentCaptor.getValue();
            assertThat(storeToCreate.getName()).isEqualTo("Boulangerie");
        }

        @Test
        void givenErrorResponse_whenCalled_createStoreWithEmptyName() throws Exception {
            // Given
            String url = "http://localhost:{port}/stores";

            Map<String, String> urlVariables = new HashMap<>();
            urlVariables.put("port", String.valueOf(port));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{" +
                    "\"name\":\"\"" +
                    "}";
            HttpEntity<String> httpEntity = new HttpEntity<>(requestJson, headers);

            when(storeService.create(createStoreArgumentCaptor.capture()))
                    .thenThrow(StoreNameCannotBeEmptyException.class);

            // When
            ResponseEntity responseEntity = template.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    String.class,
                    urlVariables
            );

            // Then
            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody()).isEqualTo(
                    "{" +
                            "\"msg\":\"name cannot be empty\""  +
                    "}"
            );

            Store storeNotCreate = createStoreArgumentCaptor.getValue();
            assertThat(storeNotCreate == null);
        }
    }

    @Nested
    class Test_getStore {

        @Test
        void givenStore_whenCalled_getStoreWithValidId() throws Exception {
            // Given
            String url = "http://localhost:{port}/stores/{id}";
            Integer id = 1;

            Map<String, String> urlVariables = new HashMap<>();
            urlVariables.put("port", String.valueOf(port));
            urlVariables.put("id", String.valueOf(id));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            Store getStore = new Store(id,"FNAC");
            when(storeService.getById(idArgumentCaptor.capture())).thenReturn(getStore);

            // When
            ResponseEntity responseEntity = template.exchange(url,
                    HttpMethod.GET,
                    httpEntity,
                    String.class,
                    urlVariables);

            // Then
            Integer idReceivedFromController = idArgumentCaptor.getValue();
            assertThat(idReceivedFromController).isEqualTo(id);

            assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
            assertThat(responseEntity.getBody()).isEqualTo(
                    "{" +
                            "\"id\":1," +
                            "\"name\":\"FNAC\"" +
                            "}"
            );
        }

        @Test
        void givenErrorResponse_whenCalled_getStoreWithInvalidId() throws Exception {
            // Given
            String url = "http://localhost:{port}/stores/{id}";
            Integer id = 1;
            Map<String, String> urlVariables = new HashMap<>();
            urlVariables.put("port", String.valueOf(port));
            urlVariables.put("id", String.valueOf(id));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(headers);

            when(storeService.getById(idArgumentCaptor.capture())).thenThrow(new ModelNotFoundException());
            // When
            ResponseEntity responseEntity = template.exchange(url,
                    HttpMethod.GET,
                    httpEntity,
                    String.class,
                    urlVariables);

            // Then
            Integer idReceivedFromController = idArgumentCaptor.getValue();
            assertThat(idReceivedFromController).isEqualTo(id);

            String errorBody = "{" +
                    "\"message\":\"Unknown store id\"" +
                    "}";

            assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
            assertThat(responseEntity.getBody()).isEqualTo(errorBody);
        }
    }
}