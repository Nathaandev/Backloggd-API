package com.example.backloggd.DTO;

import java.util.List;

public record RawgResponseDTO(

        List<RawgGameDTO>results,

        Integer count

) {}

