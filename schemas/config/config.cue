package config

#config: {
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