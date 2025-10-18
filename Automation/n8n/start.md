
```
docker pull n8nio/n8n:0.235.1
```

```
docker run -it --rm \
  -p 5678:5678 \
  -v ~/.n8n:/home/node/.n8n \
  n8nio/n8n:latest
```

```
/*/*/.n8n: permission denied.

sudo chown -R $(whoami):staff ~/.n8n
```

