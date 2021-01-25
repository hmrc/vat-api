<p>Scenario simulations using Gov-Test-Scenario headers are only available in sandbox environment.</p>
<p>Using this endpoint in the sandbox environment will store the data submitted. There is no requirement to use the periodKeys in the obligation response in the sandbox environment.</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>INVALID_VRN</p></td>
            <td><p>Submission has not passed validation. Invalid parameter VRN.</p></td>
        </tr>
        <tr>
            <td><p>INVALID_PERIODKEY</p></td>
            <td><p>Submission has not passed validation. Invalid parameter PERIODKEY.</p></td>
        </tr>
        <tr>
            <td><p>INVALID_PAYLOAD</p></td>
            <td><p>Submission has not passed validation. Invalid parameter Payload.</p></td>
        </tr>
        <tr>
            <td><p>DUPLICATE_SUBMISSION</p></td>
            <td><p>The remote endpoint has indicated that VAT has already been submitted for that period.</p></td>
        </tr>
        <tr>
            <td><p>TAX_PERIOD_NOT_ENDED</p></td>
            <td><p>The remote endpoint has indicated that the submission is for a tax period that has not ended.</p></td>
        </tr>
        <tr>
            <td><p>INSOLVENT_TRADER</p></td>
            <td><p>Simulates the scenario where the client is an insolvent trader.</p></td>
        </tr>        
    </tbody>
</table>