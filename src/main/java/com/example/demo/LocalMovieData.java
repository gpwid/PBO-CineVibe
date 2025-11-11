// File: LocalMovieData.java
package com.example.demo;

import java.util.List;

// Nama field di sini HARUS sama persis
// dengan nama field di file local_database.json
public class LocalMovieData {
    String title;
    String overview;
    double vote_average;
    String certification;
    String director;
    List<LocalActorData> actors;
    List<String> backdrops;
    String poster_path; // Ini adalah path LOKAL (misal: "images/posters/123.jpg")
}