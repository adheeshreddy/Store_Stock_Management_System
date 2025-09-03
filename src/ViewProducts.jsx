import React, { useEffect, useState } from "react";
import api from "./api/axios";

const ViewProducts = () => {
  const [products, setProducts] = useState([]);
  const [searchField, setSearchField] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 5;

  useEffect(() => {
    filterProducts();
    setCurrentPage(1);
  }, [searchField]);

  const token = localStorage.getItem('token');

  const filterProducts = async () => {
    const config = {
      headers: {
         "Content-Type": "text/plain" ,
        "Authorization":`Bearer ${token}`},
      params: { product: searchField },
    };
    try {
      const ress = await api.get("/product", config);
      setProducts(ress.data);
    } catch (err) {
      alert("Failed to Fetch Products");
      console.log(err);
    }
  };

  const handleChange = async (e) => {
    const productId = e.target.id;
    const newStatus = e.target.value === "A" ? "I" : "A";
    const config = { headers: { "Content-Type": "text/plain" } };

    try {
      await api.put("/product/" + productId, newStatus, config);
      filterProducts();
    } catch (err) {
      alert("Failed to change Product status");
      console.log(err);
    }
  };

  const handleSearchChange = (e) => {
    setSearchField(e.target.value);
  };

  const totalPages = Math.ceil(products.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentProducts = products.slice(startIndex, startIndex + itemsPerPage);

  const handlePageChange = (page) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
    }
  };

  return (
    <div className="container mt-5">
      <h3 className="text-center fw-bold mb-4">Products Management</h3>

      <div className="d-flex justify-content-center mb-3">
        <input
          type="text"
          className="form-control"
          style={{ maxWidth: "300px" }}
          placeholder="Search by Id/Name"
          value={searchField}
          onChange={handleSearchChange}
        />
      </div>

      <div className="card p-3 shadow-sm">
        <table className="table table-bordered table-hover">
          <thead className="table-light text-center">
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Status</th>
              <th>Edit Status</th>
            </tr>
          </thead>
          <tbody className="text-center">
            {currentProducts.length === 0 ? (
              <tr>
                <td colSpan={4}>No Product Found</td>
              </tr>
            ) : (
              currentProducts.map((product) => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td>{product.name}</td>
                  <td>
                    <span
                      className={`badge ${product.status === "A" ? "bg-success" : "bg-danger"}`}
                    >
                      {product.status === "A" ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-sm btn-primary"
                      onClick={handleChange}
                      id={product.id}
                      value={product.status}
                    >
                      Change Status
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {products.length > itemsPerPage && (
          <div className="d-flex justify-content-center align-items-center gap-2 mt-3">
            <button
              className="btn btn-outline-primary btn-sm"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 1}
            >
              Prev
            </button>
            {[...Array(totalPages)].map((_, i) => (
              <button
                key={i}
                className={`btn btn-sm ${currentPage === i + 1 ? "btn-primary" : "btn-outline-primary"}`}
                onClick={() => handlePageChange(i + 1)}
              >
                {i + 1}
              </button>
            ))}
            <button
              className="btn btn-outline-primary btn-sm"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages}
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ViewProducts;