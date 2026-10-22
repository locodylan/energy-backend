import sys
import json
import numpy as np
from sklearn.linear_model import LinearRegression

# Recibe los consumos desde Spring Boot
consumos = json.loads(sys.argv[1])

# Debe haber al menos 5 datos
if len(consumos) < 5:
    print(json.dumps({
        "error": "Datos insuficientes"
    }))
    sys.exit()

X = np.array(range(len(consumos))).reshape(-1, 1)
y = np.array(consumos)

modelo = LinearRegression()
modelo.fit(X, y)

# Predicción del siguiente consumo
prediccion = modelo.predict([[len(consumos)]])[0]

promedio = float(np.mean(y))

incremento = ((prediccion - promedio) / promedio) * 100

resultado = {
    "prediccion": round(float(prediccion), 2),
    "promedio": round(promedio, 2),
    "incremento": round(float(incremento), 2),
    "estado": "ALTO" if prediccion > promedio * 1.2 else "NORMAL"
}

print(json.dumps(resultado))