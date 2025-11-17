package com.example.backloggd.DTO;

import java.util.List;

public record RawgResponseDTO(

        List<RawgAPIDTO>results,

        Integer count

) {}

