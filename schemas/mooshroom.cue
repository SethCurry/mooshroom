package mooshroom


config: {
  mqtt: {
    brokers: [...string]
    username: string
    password: string
    prefix: string
  }
  http: {
    port: int
  }
  sql: {
    url: string
  }
}


DHTMessage: {
  temperature: int
  humidity: float
}