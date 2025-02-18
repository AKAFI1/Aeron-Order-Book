package org.weareadaptive.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record JsonRequest<T>(Action action, T payload)
{
}
