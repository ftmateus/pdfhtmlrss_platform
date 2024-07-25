// let apiPrefix = process.env.NODE_ENV === "development" ? "http://localhost:8080" : "/api"
let apiPrefix = "/api"

export function testApi() {
    return fetch(`${apiPrefix}/test`)
}