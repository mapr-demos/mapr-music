import {Component, ElementRef, OnInit} from "@angular/core";
import {swaggerUIBundle, swaggerUIStandalonePreset} from "../../../polyfills";
import {AppConfig} from "../../app.config";

@Component({
  selector: 'api-reference-page',
  templateUrl: './api-reference-page.component.html'
})
export class ApiReferencePage implements OnInit {

  constructor(private _elementRef: ElementRef,
              private config: AppConfig) {
  }

  HideTopbarPlugin(): any {
    // this plugin overrides the Topbar component to return nothing
    return {
      components: {
        Topbar: function () {
          return null
        }
      }
    }
  }

  ngOnInit() {

    let swaggerJsonURL = `${this.config.apiURL}/api/1.0/swagger.json`;

    swaggerUIBundle({
      url: swaggerJsonURL,
      domNode: this._elementRef.nativeElement.querySelector('#swagger-container'),
      deepLinking: false,
      presets: [
        swaggerUIBundle.presets.apis,
        swaggerUIStandalonePreset
      ],
      plugins: [
        swaggerUIBundle.plugins.DownloadUrl,
        this.HideTopbarPlugin
      ],
      layout: 'StandaloneLayout'
    });
  }

}
