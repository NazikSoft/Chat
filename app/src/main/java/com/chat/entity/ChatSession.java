package com.chat.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazar on 31.10.17.
 */

public class ChatSession {
    private String id;
    private String title;
    private String idLastMessage;
    private List<String> messagesIds = new ArrayList<>();
}
