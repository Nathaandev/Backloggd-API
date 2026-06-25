package com.example.backloggd.DTO;

import com.example.backloggd.Models.ReviewModel;

import java.util.List;

public record GameResponseDTO (String gameName, String gameDescription, String releaseDate, String publishers, Integer metacritic , String developers, String genres, String platforms, double rating, List<ReviewModel> reviews){
}
