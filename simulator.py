import asyncio
import aiohttp
import random
import time
from datetime import datetime

URL = "http://localhost:8080/sensor/telemetry"
TRUCKS = [f"TRUCK-{i}" for i in range(1, 1000)] # Gerando 1000 caminhões

async def send_data(session, truck_id):
    is_alert = random.random() < 0.05
    if is_alert:
        temp = round(random.choice([random.uniform(-10.0, -2.1), random.uniform(8.1, 20.0)]), 2)
    else:
        temp = round(random.uniform(-2.0, 8.0), 2)

    lat = round(random.uniform(-23.6, -23.5), 6)
    lng = round(random.uniform(-46.7, -46.5), 6)

    payload = {
        "truckId": truck_id,
        "temperature": temp,
        "latitude": lat,
        "longitude": lng
    }

    try:
        async with session.post(URL, json=payload, timeout=5) as response:
            status = response.status
            # Só descomente a linha de baixo se quiser travar seu terminal de tanto print!
            # print(f"[{datetime.now().strftime('%H:%M:%S')}] {truck_id} | Status: {status}")
    except Exception as e:
        pass # Ignora erros de rede para não poluir o terminal durante o stress test

async def worker():
    print(f"INICIANDO TESTE DE STRESS MASSIVO DE DADOS EM: {URL}")
    
    # aiohttp vai forçar o envio de tudo de forma esmagadora
    async with aiohttp.ClientSession() as session:
        while True:
            # Reúne as 100 requisições
            tasks = [asyncio.create_task(send_data(session, truck_id)) for truck_id in TRUCKS]
            # Manda todas ATIRAREM EXATAMENTE AO MESMO TEMPO
            await asyncio.gather(*tasks)
            # ZERO delay real entre "ondas"

# Instala a biblioteca aiohttp e Roda
if __name__ == "__main__":
    try:
        asyncio.run(worker())
    except KeyboardInterrupt:
        print("\n Teste interrompido pelo usuário.")
