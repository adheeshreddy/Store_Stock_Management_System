import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { UserContext } from './UserContext';

import './App.css';
import AddSupplier from './AddSupplier';
import UpdateSupplier from './UpdateSupplier';
import ViewSupplier from './ViewSupplier';
import CreateProforma from './CreateProforma';
import ViewProforma from './ViewProforma';
import EditProforma from './EditProforma';
import AddProduct from './AddProduct';
import ViewProducts from './ViewProducts';
import UploadProductsExcel from './UpdateProductsExcel';
import AddPurchase from './AddPurchases';
import ViewPurchases from './ViewPurchases';
import ViewStockOut from './ViewStockOut';
import Login from './Login';
import PurchaseDetailsPage from './PurchaseDetails';
import StockOutDetails from './StockOutDetails';
import ViewStockOutx from './ViewStockOutx';
import PrivateRoute from './PrivateRoute';
import Home from './Home';
import FunctionalComp from './FuntionalComp';
import ClassComp from './ClassComp';

const App = () => {
  const [username, setUsername] = useState(localStorage.getItem("username") || "");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const handleMouseEnter = () => setIsDropdownOpen(true);
  const handleMouseLeave = () => setIsDropdownOpen(false);

  return (
    <UserContext.Provider value={{ username, setUsername }}>
      <Router>
        <div className="app-container">
          <nav className="main-header">
            <ul className="nav-links">
              {username && <li><Link to="/">Dashboard</Link></li>}
              <li className="dropdown" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                <Link>Products</Link>
                {isDropdownOpen && (
                  <div className="dropdown-content">
                    <Link to="/add/product">Add Product</Link>
                    <Link to="/view/products">View Products</Link>
                    <Link to="/add/products">Add Products</Link>
                  </div>
                )}
              </li>
              <li className="dropdown" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                <Link>Suppliers</Link>
                {isDropdownOpen && (
                  <div className="dropdown-content">
                    <Link to="/add/supplier">Add Supplier</Link>
                    <Link to="/view/supplier">View Suppliers</Link>
                  </div>
                )}
              </li>
              <li className="dropdown" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                <Link>Stock In</Link>
                {isDropdownOpen && (
                  <div className="dropdown-content">
                    <Link to="/add/purchase">Add Purchase</Link>
                    <Link to="/view/purchases">View Purchases</Link>
                  </div>
                )}
              </li>
              <li className="dropdown" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                <Link>Proforma</Link>
                {isDropdownOpen && (
                  <div className="dropdown-content">
                    <Link to="/create/proforma">Create Proforma</Link>
                    <Link to="/view/proforma">View Proforma</Link>
                  </div>
                )}
              </li>
              {/* <li className="dropdown" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                <Link>C vs F Comp</Link>
                {isDropdownOpen && (
                  <div className="dropdown-content">
                    <Link to="/class/component">Class Component</Link>
                    <Link to="/functional/component">Functional Component</Link>
                  </div>
                )}
              </li> */}
              <li className="dropdown" onMouseEnter={handleMouseEnter} onMouseLeave={handleMouseLeave}>
                <Link>Stock Out</Link>
                {isDropdownOpen && (
                  <div className="dropdown-content">
                    <Link to="/view/stockout">View Stock Outs</Link>
                  </div>
                )}
              </li>
              <li className="user">
                {username && <span className="text-white me-3">{username}</span>}
                {username && (
                  <button
                    className="btn btn-outline-light btn-sm"
                    onClick={() => {
                      localStorage.clear();
                      setUsername("");
                      window.location.href = "/";
                    }}
                  >
                    Logout
                  </button>
                )}
              </li>
            </ul>
          </nav>

          <Routes>
            <Route
              path="/"
              element={
                username ? (
                  <PrivateRoute><Home /></PrivateRoute>
                ) : (
                  <Login />
                )
              }
            />
            <Route path="/add/product" element={<PrivateRoute><AddProduct /></PrivateRoute>} />
            <Route path="/view/products" element={<PrivateRoute><ViewProducts /></PrivateRoute>} />
            <Route path="/add/products" element={<PrivateRoute><UploadProductsExcel /></PrivateRoute>} />
            <Route path="/add/purchase" element={<PrivateRoute><AddPurchase /></PrivateRoute>} />
            <Route path="/view/purchases" element={<PrivateRoute><ViewPurchases /></PrivateRoute>} />
            <Route path="/view/stockout" element={<PrivateRoute><ViewStockOut /></PrivateRoute>} />
            <Route path="/add/supplier" element={<PrivateRoute><AddSupplier /></PrivateRoute>} />
            <Route path="/update/supplier" element={<PrivateRoute><UpdateSupplier /></PrivateRoute>} />
            <Route path="/view/supplier" element={<PrivateRoute><ViewSupplier /></PrivateRoute>} />
            <Route path="/create/proforma" element={<PrivateRoute><CreateProforma /></PrivateRoute>} />
            <Route path="/view/proforma" element={<PrivateRoute><ViewProforma /></PrivateRoute>} />
            <Route path="/edit/:id" element={<PrivateRoute><EditProforma /></PrivateRoute>} />
            <Route path="/purchases/:id" element={<PrivateRoute><PurchaseDetailsPage /></PrivateRoute>} />
            <Route path="/stockout/:id/details" element={<PrivateRoute><StockOutDetails /></PrivateRoute>} />
            <Route path="/stockout/:id" element={<PrivateRoute><ViewStockOutx /></PrivateRoute>} />
            <Route path="/functional/component" element={<PrivateRoute><FunctionalComp /></PrivateRoute>} />
            <Route path="/class/component" element={<PrivateRoute><ClassComp/></PrivateRoute>} />
          </Routes>
        </div>
      </Router>
    </UserContext.Provider>
  );
};

export default App;