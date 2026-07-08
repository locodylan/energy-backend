from sqlalchemy import create_engine
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
import numpy as np

usuario = "postgres"
password = "123321"
host = "localhost"
puerto = "5432"
base_datos = "energydb"

conexion = f"postgresql://{usuario}:{password}@{host}:{puerto}/{base_datos}"

engine = create_engine(conexion)

df = pd.read_sql(
    "SELECT * FROM consumo_energia",
    engine
)

if len(df) < 5:

    print("Datos insuficientes")
    exit()

df["indice"] = range(len(df))

X = df[["indice"]]

y = df["consumo"]

modelo = RandomForestRegressor(
    n_estimators=100
)

modelo.fit(X, y)

futuro = np.array([[len(df) + 1]])

prediccion = modelo.predict(futuro)

promedio = y.mean()

if prediccion[0] > promedio * 1.3:

    mensaje = (
        f"ALERTA IA: "
        f"Consumo futuro alto "
        f"{round(prediccion[0],2)} kWh"
    )

else:

    mensaje = (
        f"Predicción estable: "
        f"{round(prediccion[0],2)} kWh"
    )

print(mensaje)