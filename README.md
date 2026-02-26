# AutoveloxApp

Android app (Java) for the **Mobile Programming Lab** (UNIVAQ, 2022/2023).  
It downloads **autovelox** data and shows them in a **list** and on an **interactive map**.

**Author:** Marco Silveri

## What it does
- Fetches autovelox data from an online JSON dataset
- Shows all entries in a list
- Opens a map view:
  - single autovelox marker (from list)
  - or all autovelox markers
- Detail screen for a selected autovelox
- User location on map (with permission) + option to highlight the nearest autovelox

## Setup
- Open the project in **Android Studio**
- Add your Google Maps key in the string resource `google_maps_api_key`
- Grant **Location** permission to enable user position features
