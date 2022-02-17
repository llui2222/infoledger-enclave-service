/**
 * Top-level API responsible for processing incoming requests.
 *
 * <p>Request handling logic moved out of main application to separate responsibilies as {@link
 * com.infoledger.crypto.enclave.app.EnclaveApplication} is mainly responsible for setting up
 * enclave application and launching server and proxy, so mainly infrastructure setup.
 *
 * <p>This handler aims to separate business logic form infrastructure setup.
 */
package com.infoledger.crypto.enclave.app.controller;
