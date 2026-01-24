package com.example.backloggd.DTO;

import java.util.List;

//Dto that will get a list of all the games got.
public record RawgResponseDTO(

        List<RawgGameDTO>results,

        Integer count

) {}

