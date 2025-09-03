import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "./api/axios";

export default function PurchaseDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadDetails = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/purchases/details`,{params:{searchTerm:id}});
      setDetails(res.data || []);
    } catch {
      alert("Failed to load details");
    } finally {
      setLoading(false);
    }
  };

  const loadProducts = async () => {
    try {
      const res = await api.get("/product", {
        headers: { "Content-Type": "text/plain" },
      });
      setProducts(res.data || []);
    } catch {
      console.error("Failed to load products");
    }
  };

  const productMap = products.reduce((map, p) => {
    map[p.id] = p.name;
    return map;
  }, {});

  useEffect(() => {
    loadDetails();
    loadProducts();
  }, [id]);

  return (
    <div className="container mt-4">
      <button className="btn btn-secondary mb-3" onClick={() => navigate(-1)}>
        ⬅ Back
      </button>

      <h4 className="fw-bold">Details for Purchase #{id}</h4>
      {loading && <div className="text-muted">Loading...</div>}

      <div className="card p-3 shadow-sm mt-2">
        <table className="table table-bordered table-hover">
          <thead className="table-light text-center">
            <tr>
              <th>Product</th>
              <th>Qty</th>
              <th>Taxable</th>
              <th>GST</th>
              <th>Total</th>
              <th>Expiry</th>
            </tr>
          </thead>
          <tbody className="text-center">
            {details.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center text-muted">
                  No details
                </td>
              </tr>
            ) : (
              details.map((d, idx) => (
                <tr key={idx}>
                  <td>
                    {d.productId} - {productMap[d.productId] || "Unknown"}
                  </td>
                  <td>{d.quantity}</td>
                  <td>{Number(d.taxableAmount).toFixed(2)}</td>
                  <td>{Number(d.gstAmount).toFixed(2)}</td>
                  <td>{Number(d.totalAmount).toFixed(2)}</td>
                  <td>{d.expiry}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}