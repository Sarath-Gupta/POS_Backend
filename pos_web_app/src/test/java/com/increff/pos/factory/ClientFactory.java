package com.increff.pos.factory; // Using the same package as your example

import com.increff.pos.entity.Client;
import org.instancio.Instancio;
import org.instancio.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Arrays;

import static org.instancio.Select.field;


public final class ClientFactory {

    private ClientFactory() {
    }

    private static final Model<Client> NEW_CLIENT_MODEL = Instancio.of(Client.class)
            .set(field(Client::getId), null)
            .toModel();

    private static final Model<Client> PERSISTED_CLIENT_MODEL = Instancio.of(Client.class)
            .generate(field(Client::getId), gen -> gen.ints().min(1))
            .toModel();


    public static Client mockNewObject(String clientName) {
        return Instancio.of(NEW_CLIENT_MODEL)
                .set(field(Client::getClientName), clientName)
                .create();
    }

    public static Client mockPersistedObject(Integer id, String clientName) {
        return Instancio.of(PERSISTED_CLIENT_MODEL)
                .set(field(Client::getId), id)
                .set(field(Client::getClientName), clientName)
                .create();
    }

    public static List<Client> createClientList() {
        return Arrays.asList(
                mockPersistedObject(1, "Client One"),
                mockPersistedObject(2, "Client Two")
        );
    }

    public static Pageable createPageable() {
        return PageRequest.of(0, 10);
    }

    public static Page<Client> createClientPage(List<Client> clientList, Pageable pageable) {
        // PageImpl takes (content, pageable, totalElements)
        return new PageImpl<>(clientList, pageable, clientList.size());
    }
}
