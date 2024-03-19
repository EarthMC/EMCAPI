# EMCAPI
Welcome to the official repository of EarthMC's API! The API's documentation can be found at https://earthmc.net/docs/api.

# Contributing
If you would like to contribute to the repo, you are welcome to open a pull request provided it doesn't introduce a breaking API change.

Reasons you may open a pull request include but are not limited to:
- Adding data that is not already included
- Adding functionality that is not already present
- Fixing an error or failure to meet the design methodology (defined below)
- Rewriting code to deduplicate repetition or improve performance

## Design Methodology
- If data can return as null or empty such as an empty string, the JSON element should under most circumstances be set to a null value. Arrays are exempted from this and should be returned as an empty array.
- JSON patterns should be repeated throughout responses. Objects should be in the same order if they are shared between any responses, and keys that represent similar or same things such as mayor and king should be in the same or similar spot. This is to keep working with different response types consistent and easily understandable for developers.
